(ns defect-link-corrector.corrector-test
  (:require [clojure.test :refer :all]
            [defect-link-corrector.corrector :refer :all]
            [defect-link-corrector.db-test :refer :all])
  (:use org.httpkit.fake))

(def node {:nid 1 :status 1 :title "node1" :type "node" :body "<a href=\"http://fancy-url.com/Some\">multiple redirects absolute</a><a href=\"wut-da-fuck\""})

(deftest link-corrector-test
  (let [node (->> test-data
                   :node-rev
                   (filter #(.contains (:body %) "href"))
                   first)]
    (testing "should attach links to node"
      (is (= (->> (attach-links :origin node) :links :origin nil? not) true)))))


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
           false (can-correct? can-not-200)))))


(deftest correct-body-test
  (let [url-maps '({:status 200
                    :orgin-url "http://fancy-url.com"
                    :url-relative "/"
                    }
                   {:status 404
                    :origin-url "/no-such-path"
                    :url-relative "/no-such-path"})
        body "<a href=\"/no-such-path\">/no-such-path</a><a href=\"http://fancy-url.com\">http://fancy-url.com</a>"]
    (testing "replacement of href in text"
      (is (= (correct-body body url-maps)
         "<a href=\"/no-such-path\">/no-such-path</a><a href=\"/\">http://fancy-url.com</a>")))))

(deftest gen-href-test
  (let [url "http://www.fancy.com"
        result (gen-href url)]
    (testing "valid href attribute should be generated"
      (is (= "href=\"http://www.fancy.com\"" result)))))
