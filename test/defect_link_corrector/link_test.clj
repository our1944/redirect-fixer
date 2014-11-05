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
      (is (= (count links)
           2)))))
