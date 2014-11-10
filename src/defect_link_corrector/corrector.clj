(ns defect-link-corrector.corrector
  (:require [defect-link-corrector.link :as link]))

(defn attach-links
  "take a key and drupal node, attach original links found to the node"
  [k node]
  (assoc-in node [:links k] (link/extract-links (:body node))))

(defn- get-new-url
  "return a partial function of link/process-origin-url with prefix applied"
  [prefix]
  (partial link/process-origin-url prefix))

(defn can-correct?
  "take an url-map as returned by link/process-origin-url, determine if can be fixed"
  [url-map]
  (let [status (:status url-map)
        origin-url (:origin-url url-map)
        url-relative (:url-relative url-map)]
  (and  (= 200 status)
        (not (= origin-url url-relative)))))

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
            (if (can-correct? url)
              (clojure.string/replace res
                                      (gen-href (:orgin-url url))
                                      (gen-href (:url-relative url)))
              res))
          text
          url-maps))

(defn process-nodes
  "take list of drupal node maps, attach links to each one"
  [prefix nodes]
  (reduce (fn [acc curr]
            (let [origin-links (link/extract-links curr)
                  new-links (->> origin-links
                                 (map #(-> % (get-in [:attrs :href] get-new-url))))]
              (cond
               (empty? origin-links) acc
               :else (conj acc (assoc curr :links new-links)))))
          []
          nodes))
