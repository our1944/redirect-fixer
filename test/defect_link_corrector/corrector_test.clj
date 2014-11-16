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


(deftest correct-body-test
  (let [url-maps '({:status 200
                    :origin-url "http://fancy-url.com"
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

(deftest produce-node-links-test
  (let [url-maps '({:status 200
                     :origin-url "http://fancy-url.com"
                     :url-relative "/"
                     }
                    {:status 404
                     :origin-url "/no-such-path"
                     :url-relative "/no-such-path"}
                    {:status 200
                     :origin-url "/"
                     :url-relative "/"})
        body "<a href=\"/no-such-path\">/no-such-path</a><a href=\"http://fancy-url.com\">http://fancy-url.com</a><a href=\"/\">home</a>"
        node {:nid 1
              :status 1
              :type "node"
              :body body
              :links url-maps}
        processed (produce-node-links node)]
    (testing "only errors will be kept and produce product with node"
      (are [x y] (= x y)
           2 (count processed)
           true (every? :nid processed)))))

(deftest process-nodes-test
  (let [body "<a href=\"/no-such-path\">/no-such-path</a><a href=\"http://fancy-url.com\">http://fancy-url.com</a><a href=\"/\">home</a>"
        nodes [{:nid 1
                :status 1
                :type "node"
                :body body}]
        body-mem (atom "")
        db-update-func (fn [b] (compare-and-set! body-mem @body-mem (:body b)))
        prefix "http://www.fancy-url.com"
        fake-options [(str prefix "/") {:status 200
                              :opts {:url prefix}}
                      (str prefix "/no-such-path") {:status 404
                                                    :opts {:url (str prefix "/no-such-path")}}
                      "http://fancy-url.com" {:status 200
                                              :opts {:trace-redirects '("http://fancy-url.com")
                                                     :url prefix}}]
        res (with-fake-http fake-options
              (process-nodes prefix nodes db-update-func))
        ]
    (testing "process-node should replace invalid href with valid href if possible"
      (is (= "<a href=\"/no-such-path\">/no-such-path</a><a href=\"/\">http://fancy-url.com</a><a href=\"/\">home</a>"
             @body-mem)))
    (testing "check required k-v in result vector"
      (is false)) ; correct me
    ))
