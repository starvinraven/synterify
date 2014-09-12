(ns synterify.s3
  (:require [aws.sdk.s3 :as s3]
            [digest :as digest]))

(defonce cred {:access-key (System/getenv "S3_KEY"), :secret-key (System/getenv "S3_SECRET")})

(defonce bucket "synterify-images")

(s3/create-bucket cred bucket)

(defn- get-key-by-url
  [url]
  (digest/sha-256 url))

(defn put-image
  [url file]
  (let [key (get-key-by-url url)]
    (s3/put-object cred bucket key file)
    (s3/update-object-acl cred bucket key (s3/grant :all-users :read))))

(defn image-exists?
  [url]
  (s3/object-exists? cred bucket (get-key-by-url url)))

(defn get-image
  [url]
  (:content
   (s3/get-object cred bucket (get-key-by-url url))))

