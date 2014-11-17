(ns defect-link-corrector.output-test
  (:require [clojure.test :refer :all]
            [defect-link-corrector.output :refer :all]))

(def nodes [{:nid 1 :type "page" :status 1 :title "node 1" :origin-url "/Blah" :replaced true :url-relative "/blah" :res-status 200}
            {:nid 2 :type "node" :status 1 :title "node 2" :origin-url "/404" :replaced false :url-relative nil :res-status 404}])

(def head "nid;type;status;title;origin-url;replaced;url-relative;res-status")
(def n1 "1;page;1;node 1;/Blah;true;/blah;200")
(def n2 "2;node;1;node 2;/404;false;;404")

(deftest write-csv-test
  (let [baos (new java.io.ByteArrayOutputStream)]
    (testing "data is empty"
      (.reset baos) ; clear content of output stream
      (with-open [w (clojure.java.io/writer baos)]
        (write-csv [] :out-writer w))
      (is (= (str head "\n") (.toString baos))))
    (testing "sample data"
      (.reset baos)
      (with-open [w (clojure.java.io/writer baos)]
        (write-csv nodes :out-writer w))
      (is (= (str head "\n" n1 "\n" n2 "\n") (.toString baos))))))
