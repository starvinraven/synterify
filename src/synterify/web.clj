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

(defn stop-server!
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server!
  []
  (reset! server (httpkit/run-server (handler/site #'routes) {:port 8080})))

(defn- handle-request
  [url]
  (when url
    {:status  200
     :headers {"Content-Type" "image/jpeg"}
     :body    (image/combinate url)}))