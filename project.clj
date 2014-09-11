(defproject synterify "0.1.0-SNAPSHOT"
            :description "FIXME: write description"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.5.1"]
                           [http-kit "2.1.16"]
                           [compojure "1.1.9"]
                           [javax.servlet/servlet-api "2.5"]]
            :plugins [[org.clojars.shishkin/lein-lesscss "1.3.3"]
                      [lein-coffeescript "0.1.1"]
                      [lein-npm "0.4.0"]]
            :lesscss-paths ["resources/less"]
            :lesscss-output-path "resources/public/css"
            :node-dependencies [[coffee-script "1.8.0"]]
            :coffeescript {:sources "coffee-src/*.coffee"
                           :output "resources/public/js"}
            :hooks [lein-coffeescript.plugin]
            :main synterify.core
            :profiles {:uberjar {:aot :all}})
