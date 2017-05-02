(ns synterify.web
  (:require [org.httpkit.server :as httpkit]
            [compojure.core :as compojure]
            [ring.middleware.multipart-params :as multipart]
            [ring.middleware.json :as ring-json]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [synterify.image :as image]
            [synterify.s3 :as s3]))

(declare handle-request handle-post cache-and-return)

(defonce server (atom nil))

(compojure/defroutes routes
                     (->
                       (compojure/POST "/doit" {params :params} (handle-post (-> params :file :tempfile) (:url params) (keyword (:overlay-name params))))
                       (multipart/wrap-multipart-params)
                       (ring-json/wrap-json-response))
                     (compojure/GET "/batmanterify" [:as {params :params}] (handle-request (:url params) :batmanterify))
                     (compojure/GET "/doit" [:as {params :params}] (handle-request (:url params) :synterify))
                     (compojure/GET "/get" [:as {params :params}] (handle-request (:url params) (keyword (:overlay-name params))))
                     (compojure/GET "/" [] (resp/resource-response "index.html" {:root "public"}))
                     (route/resources "/")
                     (route/not-found "<p>Page not found.</p>"))

(defn- get-base-url
  []
  (or (System/getenv "BASE_URL") "http://localhost:8080"))

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
  {:status  200
   :headers {"Content-Type" "image/jpeg"}
   :body    body})

(defn- store-bytes
  [img-file url overlay-name]
  (let [file (image/combinate-bytes img-file url overlay-name)]
    (s3/put-image url file overlay-name)))

(defn- cache-and-return
  [url overlay-name]
  (println "cache-and-return" url)
  (let [file (image/combinate-url url overlay-name)]
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

(defn- post-response
  [url]
  (resp/response {:status 200
                  :body   {:url url}}))

(defn- handle-post-request
  [img-file url overlay-name]
  (when (not (s3/image-exists? url overlay-name))
    (store-bytes img-file url overlay-name))
  (println "resp" (get-base-url) url overlay-name)
  (post-response (str (get-base-url) "/get?url=" url "&overlay-name=" (name overlay-name))))

(defn- handle-post
  [img-file url overlay-name]
  (println "handle post" img-file url overlay-name)
  (if (and img-file url overlay-name)
    (handle-post-request img-file url overlay-name)
    {:status 400}))