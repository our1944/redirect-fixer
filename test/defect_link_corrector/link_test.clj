(ns defect-link-corrector.link-test
  (:require [clojure.test :refer :all]
            [defect-link-corrector.link :refer :all]
            [org.httpkit.client :as http])
  (:use org.httpkit.fake))

(deftest prob-url-test
  (let [url "http://fancy-url"
        res (with-fake-http [url {:opts {:url url}
                                  :status 200}]
              (prob-url url))]

    (testing "valid 200 url without redirect should have nil for trace-redirects"
      (is (= (:trace-redirects res)
             nil)))))

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
