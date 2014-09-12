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

(defn image-response
  [body]
  {:status 200
   :headers {"Content-Type" "image/jpeg"}
   :body body})

(defn- cache-and-return
  [url]
  (println "cache-and-return" url)
  (let [file (image/combinate url)]
    (s3/put-image url file)
    file))

(defn- handle-request
  [url]
  (when url
    (if (s3/image-exists? url)
      (do
        (println "using cached image" url)
        (image-response (s3/get-image url)))
      (image-response (cache-and-return url)))))