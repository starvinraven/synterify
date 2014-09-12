(defproject synterify "0.1.0-SNAPSHOT"
            :description "FIXME: write description"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.5.1"]
                           [http-kit "2.1.16"]
                           [compojure "1.1.9"]
                           [javax.servlet/servlet-api "2.5"]
                           [clj-aws-s3 "0.3.10"]
                           [digest "1.4.4"]]
            :plugins [[org.clojars.shishkin/lein-lesscss "1.3.3"]]
            :lesscss-paths ["resources/less"]
            :lesscss-output-path "resources/public/css"
            :node-dependencies [[coffee-script "1.8.0"]]
            :main synterify.core
            :min-lein-version "2.0.0"
            :profiles {:uberjar {:aot :all}})
