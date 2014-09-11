(ns synterify.web
  (:require [org.httpkit.server :as httpkit]
            [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [synterify.image :as image]))

(declare handle-request)

(defonce server (atom nil))

(compojure/defroutes routes
           (compojure/GET "/doit" [:as {params :params}] (handle-request (:url params)))
           (compojure/GET "/" [] (resp/resource-response "index.html" {:root "public"}))
           (route/resources "/")
           (route/not-found "<p>Page not found.</p>"))

(defn- get-port
  []
  (Integer. (or (System/getenv "PORT") "8080")))

(defn stop-server!
  []
  (when-not (nil? @server)
    (do
      (println "stopping server")
      (@server :timeout 100))
    (reset! server nil)))

(defn start-server!
  []
  (reset! server (httpkit/run-server (handler/site #'routes) {:port (get-port)}))
  (println "server started on port" (get-port)))

(defn- handle-request
  [url]
  (when url
    {:status  200
     :headers {"Content-Type" "image/jpeg"}
     :body    (image/combinate url)}))