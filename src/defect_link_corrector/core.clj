(ns defect-link-corrector.core
  (:gen-class)
  (require [clojure.edn :as edn]
           [defect-link-corrector.db :as db]
           [defect-link-corrector.corrector :as corrector]
           [defect-link-corrector.output :as output]))

(def file-separator (System/getProperty "file.separator"))
(def home-dir (System/getProperty "user.home"))

(def config-files ["config.edn" (clojure.string/join file-separator [home-dir ".defect_link_corrector" "config.edn"])])

(def config
  (when-let [config-file (->> config-files
                              (filter #(.exists (clojure.java.io/as-file %)))
                              first)]
    (edn/read-string (slurp config-file))))

(defn config-valid?
  [config-map]
  (every? #(-> (% config-map) nil? not) '(:db :prefix)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (config-valid? config)
    (let [prefix (:prefix config)
          db-spec (:db config)
          nodes (db/get-nodes db-spec)
          update-body-fn (partial db/update-node-body db-spec)
          results (corrector/process-nodes prefix nodes update-body-fn)]
      (output/write-csv results))
    ((println "Configuration file not valid!")
     (System/exit 1))))
