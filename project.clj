(defproject com.wsscode/js-interop "0.0.1"
  :description "Library to provide alternative tools to interop between Clojurescript immutable data structures and JSON."
  :url "https://github.com/wilkerlucio/js-data-interop"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.764" :scope "provided"]]

  :jar-exclusions [#"resources/.*" #"node-modules/.+"]

  :deploy-repositories [["clojars" {:url   "https://clojars.org/repo/"
                                    :creds :gpg :checksum :ignore}]
                        ["releases" :clojars]
                        ["snapshots" :clojars]])
