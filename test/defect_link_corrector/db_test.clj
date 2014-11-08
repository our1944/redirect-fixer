(ns defect-link-corrector.db-test
  (:require [defect-link-corrector.db :refer :all]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]))

(def test-data {:node [{:nid 1, :status 1, :title "node 1", :type "node"}
                       {:nid 2, :status 1, :title "node 2", :type "node"}
                       {:nid 3, :status 1, :title "node 3", :type "node"}
                       {:nid 4, :status 1, :title "node 4", :type "node"}
                       {:nid 5, :status 1, :title "node 5", :type "node"}
                       {:nid 6, :status 1, :title "node 6", :type "node"}]
                :node-rev [{:nid 1, :body "<a href=\"/de\">home</a>"}
                           {:nid 2, :body "<a href=\"/Croatia\">croatia</a><div><a href=\"/de\">home</a></div>"}
                           {:nid 3, :body "<a href=\"www.yachtico.com\">error without protocol</a>"}
                           {:nid 4, :body "<a href=\"http://yachtico.com\">absolute</a>"}
                           {:nid 5, :body "<a href=\"http://yachtico.com/Croatia\">multiple redirects absolute</a>"}
                           {:nid 6, :body "<a href=\"/wut-da-fuck\">404</a>"}
                           ]})


(def db-spec {:classname "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname "/tmp/test.db"
              })

(defn create-db []
  (try (jdbc/db-do-commands db-spec
                           (jdbc/create-table-ddl :node
                                                  [:nid :int]
                                                  [:status :int]
                                                  [:type :text]
                                                  [:title :text])
                           (jdbc/create-table-ddl :node_revisions
                                                  [:nid :int]
                                                  [:body :text]))
       (catch Exception e (println e))))

(defn insert-test-data [td]
  (let [db-con (jdbc/get-connection db-spec)]
    (jdbc/with-db-transaction [db-con db-spec]
      (doseq [n (:node td)]
        (jdbc/insert! db-con :node n))
      (doseq [nv (:node-rev td)]
        (jdbc/insert! db-con :node_revisions nv)))))

(defn remove-db-file []
  (try (-> "/tmp/test.db" java.io.File. .delete)
       (catch Exception e (println e))))


(deftest db-ops
  (testing "try to prepare db"
    (try
      (create-db)
      (insert-test-data test-data)
      (update-nodes-body db-spec [{:nid 1 :body "updated"}])
      (testing "update body should works"
        (let [node1 (->> db-spec
                         get-nodes
                         (filter #(= (:nid %)))
                         first)]
        (is (= (:body node1) "updated"))))
      (catch Exception e (println e))
      (finally (remove-db-file)))))
