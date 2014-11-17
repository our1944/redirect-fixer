(ns defect-link-corrector.output
  (:require [clojure.data.csv :as csv]))

(def node-keys [:nid :type :status :title :origin-url :replaced :url-relative :res-status])

(defn write-csv
  [nodes & {:keys [out-writer] :or {out-writer *out*}}]
  (let [header (map name node-keys)
        content (map (fn [x] (map #(% x) node-keys)) nodes)
        data (conj (apply list content) header)]
  (csv/write-csv out-writer data :separator \;)))
