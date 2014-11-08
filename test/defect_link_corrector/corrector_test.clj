(ns defect-link-corrector.corrector-test
  (:require [clojure.test :refer :all]
            [defect-link-corrector.corrector :refer :all]
            [defect-link-corrector.db-test :refer :all])
  (:use org.httpkit.fake))

(deftest link-corrector-test
  (let [node (->> test-data
                   :node-rev
                   (filter #(.contains (:body %) "href"))
                   first)]
    (testing "should attach links to node"
      (is (= (->> (attach-links :origin node) :links :origin nil? not) true)))))


(deftest add-new-link-to-nodes-test
  (let [prefix "http://fancy-domain"]
    ))
