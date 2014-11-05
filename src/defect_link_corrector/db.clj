(ns defect-link-corrector.db
  (:require [clojure.java.jdbc :as jdbc]))

(defn get-nodes
  "function which try to connect drupal db and get specific nodes"
  [db-spec]
  (jdbc/query db-spec
              [(str "SELECT n.nid, n.title, n.status, n.type, nr.body FROM node n "
                    "JOIN node_revisions nr ON n.nid = nr.nid "
                    "WHERE n.status = 1 AND nr.body <> ''")]))
