(ns defect-link-corrector.link-test
  (:require [clojure.test :refer :all]
            [defect-link-corrector.link :refer :all]
            [org.httpkit.client :as http])
  (:use org.httpkit.fake))

(deftest prob-url-test
  (let [url "http://fancy-url"
        res (with-fake-http [url {:opts {:url url}
                                  :status 200}]
              (prob-url url))
        redirected (with-fake-http [url {:opts {:url (str url ".com")
                                                :trace-redirects (list url)}
                                         :url (str url ".com")}]
                     (prob-url url))]

    (testing "valid 200 url without redirect should have nil for trace-redirects"
      (is (= (:trace-redirects res)
             nil)))
    (testing "redirected should contains trace-redirects"
      (are [x y] (= x y)
           url (-> redirected
                   :trace-redirects
                   first)
           "http://fancy-url.com" (:url redirected)))))

(deftest extract-link-test
  (let [nested-text "<div>outer <a href=\"/fancy-link\">fancy</a>
<div>inner <a href=\"another-link\">another</a></div></div>"
        links (extract-links nested-text)]
    (testing "extract-links should get all links out, even nested"
      (are [x y] (= x y)
           (count links) 2
           (-> links first (get-in [:attrs :href])) "/fancy-link"))))

(deftest to-absolute-test
  (let [prefix "http://www.yachtico.com"
        relative "/de/croatia"
        absolute "http://yachtico.com/de"
        without-protocol "www.yachtico.com/yacht-charter-rentals-greece"]
    (testing "to-absolute should be able to handle all situations defined here"
      (are [x y] (= x y)
           "http://www.yachtico.com" (to-absolute prefix prefix)
           "http://www.yachtico.com/de/croatia" (to-absolute prefix relative)
           "http://yachtico.com/de" (to-absolute prefix absolute)
           "www.yachtico.com/yacht-charter-rentals-greece" (to-absolute prefix without-protocol)))))

(deftest to-relative-test
  (let [prefix "http://www.yachtico.com"
        relative "/de/croatia"
        absolute (str prefix "/de")]
    (testing "to-relative should only convert absolute to relative"
      (are [x y] (= x y)
           "/de" (to-relative prefix absolute)
           "/de/croatia" (to-relative prefix relative)
           "/" (to-relative prefix prefix)))))

(deftest new-url-to-relative-test
  (let [prefix "http://fancy-url.com"
        resp {:status 200
              :url prefix
              :trace-redirects '("http://fancy-url")}
        processed (new-url-to-relative prefix resp)]
    (testing "new-url-to-relative-test"
      (is (= "/" (:url-relative processed))))))

(deftest process-origin-url-test
  (let [prefix "http://fancy-url.com"
        url "http://fancy-url"
        resp {:status 200
              :url prefix
              :trace-redirects (list url)}
        processed (with-fake-http [url {:opts {:url (str url ".com")
                                                :trace-redirects (list url)}
                                         :url (str url ".com")}]
                     (process-origin-url prefix "http://fancy-url"))]
    (testing "check if result map is valid"
      (are [x y] (= x y)
           "/" (:url-relative processed)
           "http://fancy-url" (:origin-url processed)))))

(deftest problematic-links-only-test
  (let [urls '({:origin-url "http://fancy-url"
                :url-relative "/"
                :url "http://fancy-url.com"
                :trace-redirects '("http://fancy-url")
                :status 200}
               {:origin-url "fancy-url"
                :url-relative "fancy-url"
                :status 404}
               {:origin-url "http://fancy-url.com"
                :url "http://fancy-url.com"
                :url-relative "/"
                :status 200})]
    (testing "problematic-url-only should filter out health links"
      (is (= 2 (-> urls
                   problematic-links-only
                   count))))))

(deftest can-correct-test
  (let [can {:status 200
             :origin-url "http://fancy-url.com"
             :url-relative "/"}
        can-not {:status 404
                 :origin-url "/no-such-thing"
                 :url-relative "/no-such-thing"
                 }
        can-not-200 {:status 200
                     :origin-url "/some-path"
                     :url-relative "/some-path"}]
    (testing "only status 200 and origin-url not equals to url-relative can be corrected"
      (are [x y] (= x y)
           true (can-correct? can)
           false (can-correct? can-not)
           false (can-correct? can-not-200)))
    (testing "links with status other than 200 will be treated as not correctable"
      (are [x y] (= x y)
           false (is-error? can-not-200)
           false (recoverble-error? can-not-200)
           false (recoverble-error? can-not)))))
