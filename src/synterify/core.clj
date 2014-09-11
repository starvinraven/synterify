(ns synterify.core
  (:require [synterify.web :as web]))

(defn -main
  []
  (web/start-server!)
  (println "server running"))
