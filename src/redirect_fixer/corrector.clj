(ns redirect-fixer.corrector
  (:require [redirect-fixer.link :as link]
            [clj-progress.core :as clj-progress]))

(defn attach-links
  "take a key and drupal node, attach original links found to the node"
  [k node]
  (assoc-in node [:links k] (link/extract-links (:body node))))

(defn get-new-url
  "return a partial function of link/process-origin-url with prefix applied"
  [prefix]
  (partial link/process-origin-url prefix))

(defn gen-href
  "take an url and generate a href"
  [url]
  (cond
   (not (nil? url)) (str "href=\"" url "\"")
   :else "href=\"\""))

(defn correct-body
  "take a piece of text and replace any url that could be replaced"
  [text url-maps]
  (reduce (fn [res url]
            (if (link/can-correct? url)
              (clojure.string/replace res
                                      (gen-href (:origin-url url))
                                      (gen-href (:url-relative url)))
              res))
          text
          url-maps))

(defn- process-url-map
  [url-map]
  (let [res-status (:status url-map)]
    (-> url-map
        (assoc :res-status res-status)
        (dissoc :status))))

(defn produce-node-links
  "take a node and build return node * (:links node), contains only error links"
  [node]
  (let [links (:links node)]
    (if (-> links sequential?)
      (reduce (fn [acc link]
                (let [l (cond
                         (not (link/is-error? link)) nil
                         (link/can-correct? link) (assoc link :replaced true)
                         (not (link/recoverble-error? link)) (assoc link :replaced false))]
                  (if (nil? l)
                    acc
                    (conj acc (merge node (process-url-map l))))))
              '()
              links)
      node)))

(defn process-nodes
  "take list of drupal node maps, attach links to each one
  db-update-func should be a function which takes a node map and update body in node_revisions table"
  [prefix nodes db-update-func]
  (clj-progress/init "processing nodes" (count nodes))
  (def res (reduce (fn [acc curr]
            (clj-progress/tick)
            (let [new-url-fn (get-new-url prefix)
                  old-body (:body curr)
                  origin-links (link/extract-links old-body)
                  new-links (map #(new-url-fn (get-in % [:attrs :href])) origin-links)
                  new-node (assoc curr :links new-links)
                  new-body (correct-body (:body curr) new-links)]
              (if (not (= new-body old-body)) ; warning, side-effect to update db needed
                (db-update-func (assoc new-node :body new-body))) ; update-body
              (cond
               (empty? origin-links) acc
               :else (into acc (produce-node-links new-node)))))
          []
          nodes))
  (clj-progress/done res))
