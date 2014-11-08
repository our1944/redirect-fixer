(ns defect-link-corrector.corrector
  (:require [defect-link-corrector.link :as link]))

(defn attach-links
  "take a key and drupal node, attach original links found to the node"
  [k node]
  (assoc-in node [:links k] (link/extract-links (:body node))))

(defn- get-new-url
  [prefix]
  (partial link/convert-origin-url prefix))

(defn add-new-link-to-nodes
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
