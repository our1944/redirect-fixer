(ns redirect-fixer.db.component
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [redirect-fixer.db.db :as db]
            [clojure.core.async :refer [chan go >! onto-chan]]))

(defrecord Database [db-spec channels]
  ;; Implments the LifeCycle protocol
  component/LifeCycle

  (start [component]
    (println ";; starting Database")
    ;; get all rows to be changed and put to out channels
    (let [read-chan   (:read-result-chan channels)
          write-chan  (:write-chan channels)
          final-chan  (:final-results-chan channels)]
      ;; putting the result of db query into read-chan
      (onto-chan read-chan (db/get-nodes db-spec))
      ;; todo: connecting logic with other two chans
      ))

  (stop [component]
    (println ";; stoping Database")))

(defn new-database [config]
  ;; simple constructor for Database component
  (map->Database (:db-spec config)))

(defrecord DBChannels []
  ;; Implements the LifeCycle protocol
  component/LifeCycle

  (start [component]
    (println ";; starting DBChannels")
    (assoc component
      :read-result-chan (chan)
      :write-chan (chan)
      :final-result-chan (chan)))

  (stop [component]
    (println ";; stopping DBChannels")
    (assoc component
      :read-result-chan nil
      :write-chan nil
      :final-results-chan nil)))

(defn new-db-channels []
  (map->DBChannels {}))
