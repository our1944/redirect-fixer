(ns defect-link-corrector.output
  (:require [clojure.data.csv :as csv]))

(defn node-keys [:nid :type :status :title :origin-url :replaced :url-relative :status])

(defn write-csv
  [nodes & {:keys [out-writer] :or {out-writer *out*}}]
  )
