(ns defect-link-corrector.output-test
  (:require [clojure.test :refer :all]
            [defect-link-corrector.output :refer :all]))

(def nodes [{:nid 1 :type "page" :status 1 :title "node 1" :origin-url "/Blah" :replaced true :url-relative "/blah" :res-status 200}
            {:nid 2 :type "node" :status 1 :title "node 2" :origin-url "/404" :replaced false :url-relative nil :res-status 404}])

(def head "nid;type;status;title;origin-url;replaced;url-relative;res-status")

(deftest write-csv-test
  (let [baos (new java.io.ByteArrayOutputStream)]
    (testing "data is empty"
      (.reset baos) ; clear content of output stream
      (with-open [w (clojure.java.io/writer baos)]
        (write-csv [] :out-writer w))
      (is (= (str head "\n") (.toString baos))))))
