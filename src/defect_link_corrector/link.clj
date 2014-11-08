(ns defect-link-corrector.link
  (:require [org.httpkit.client :as http]
            [net.cgrand.enlive-html :as html]))

(defn prob-url
  "take an url and try access it
  return a map contains status, new url, and redirect trace
  {:status 200
   :url \"http://some-fancy-url\"
   :trace-redirects (\"redirect-traget\" \"redirect-target\")}"
  [url]
  (let [{:keys [status opts] :as resp} @(http/get url)]
       {:status status
        :url (:url opts)
        :trace-redirects (:trace-redirects opts)}))


(defn extract-links
  "generate a seq of links contained in a html snippet"
  [body]
  (let [snippet (html/html-snippet body)]
    (html/select snippet [:a])))

(defn to-absolute
  "deterine if url is "
  [prefix url]
  (cond
   (re-find #"^http://" url) url
   (re-find #"^/" url) (str prefix url)
   :else url))

(defn to-relative
  [prefix url]
  (let [relative (clojure.string/replace url prefix "")]
    (if (= "" relative)
      "/"
      relative)))

(defn new-url-to-relative
  [prefix url-map]
  (assoc url-map (to-relative prefix (:url url-map))))

(defn convert-origin-url
  [prefix origin-url]
  (let [absolute (partial to-absolute prefix)
        result-map (->> origin-url
                        absolute
                        prob-url
                        (new-url-to-relative prefix))]
    (assoc result-map :origin-url origin-url)))
