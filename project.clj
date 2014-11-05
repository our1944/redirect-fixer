(defproject defect-link-corrector "0.1.0-SNAPSHOT"
  :description "connect to drupal db and replace defect urls in node bodies"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [http-kit "2.1.16"]
                 [http-kit.fake "0.2.1"]
                 [enlive "1.1.5"]]
  :main ^:skip-aot defect-link-corrector.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-auto "0.1.1"]
            [cider/cider-nrepl "0.8.0-SNAPSHOT"]])
