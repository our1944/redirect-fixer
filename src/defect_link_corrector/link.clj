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
    (html/select snippet [[:a (html/attr? :href)]])))

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
  (assoc url-map :url-relative (to-relative prefix (:url url-map))))

(defn process-origin-url
  [prefix origin-url]
  (let [absolute-fn (partial to-absolute prefix)
        result-map (->> origin-url
                        absolute-fn
                        prob-url
                        (new-url-to-relative prefix))]
    (assoc result-map :origin-url origin-url)))

(defn problematic-links-only
  "take a coll of url-maps returned by link/process-origin-url
  only keep the status not 200 or with redirects"
  [url-maps]
  (filter (fn [curr]
            (or
             (-> curr :status (= 200) not)
             (-> curr :trace-redirects empty? not)))
          url-maps))

(defn can-correct?
  "take an url-map as returned by process-origin-url, determine if can be fixed"
  [url-map]
  (let [status (:status url-map)
        origin-url (:origin-url url-map)
        url-relative (:url-relative url-map)]
    (and  (= 200 status)
          (not (= origin-url url-relative)))))

(defn is-error?
  "take an url-map as returned by process-origin-url, determine if it contains error"
  [url-map]
  (let [status (:status url-map)
        origin-url (:origin-url url-map)
        url-relative (:url-relative url-map)]
    (or (not (= 200 status))
        (not (= origin-url url-relative)))))

(defn recoverble-error?
  "take url-map as returned by process-origin-url, determine if recoverble"
  [url-map]
  (and (is-error? url-map)
       (can-correct? url-map)))
