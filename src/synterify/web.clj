(ns synterify.web
  (:require [org.httpkit.server :as httpkit]
            [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [synterify.image :as image]
            [synterify.s3 :as s3]))

(declare handle-request)

(defonce server (atom nil))

(compojure/defroutes routes
           (compojure/GET "/batmanterify" [:as {params :params}] (handle-request (:url params) :batmanterify))
           (compojure/GET "/doit" [:as {params :params}] (handle-request (:url params) :synterify))
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

(defn image-response
  [body]
  {:status 200
   :headers {"Content-Type" "image/jpeg"}
   :body body})

(defn- cache-and-return
  [url overlay-name]
  (println "cache-and-return" url)
  (let [file (image/combinate url overlay-name)]
    (s3/put-image url file overlay-name)
    file))

(defn- handle-request
  [url overlay-name]
  (when url
    (if (s3/image-exists? url overlay-name)
      (do
        (println "using cached image" url)
        (image-response (s3/get-image url overlay-name)))
      (image-response (cache-and-return url overlay-name)))))