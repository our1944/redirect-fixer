(ns redirect-fixer.core
  (:gen-class)
  (require [clojure.edn :as edn]
           [redirect-fixer.db :as db]
           [redirect-fixer.corrector :as corrector]
           [redirect-fixer.output :as output]))

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
  (cond
   (-> config config-valid? not) ((println "config file not valid!")
                                  (System/exit 1))
   (-> args empty?) ((println "output file must be given")
                     (System/exit 1))
   (let [f (clojure.java.io/as-file (first args))]
     (and (-> f .canWrite not)
         (-> f .createNewFile not))) ((println "output file not writeable")
                                      (System/exit 1))
   :else (let [prefix (:prefix config)
               db-spec (:db config)
               nodes (db/get-nodes db-spec)
               update-body-fn (partial db/update-node-body db-spec)
               results (corrector/process-nodes prefix nodes update-body-fn)]
           (with-open [w (clojure.java.io/writer (first args) :append false :encoding "UTF8")]
             (output/write-csv results :out-writer w)))))
