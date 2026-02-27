(defproject com.github.igrishaev/any "0.1.1-SNAPSHOT"

  :description
  "Objects for smart comparison in tests."

  :url
  "https://github.com/igrishaev/any"

  :deploy-repositories
  {"releases" {:url "https://repo.clojars.org" :creds :gpg}}

  :license
  {:name "The Unlicense"
   :url "https://choosealicense.com/licenses/unlicense/"}

  :release-tasks
  [["vcs" "assert-committed"]
   ["test"]
   ["change" "version" "leiningen.release/bump-version" "release"]
   ["vcs" "commit"]
   ["vcs" "tag" "--no-sign"]
   ["deploy"]
   ["change" "version" "leiningen.release/bump-version"]
   ["vcs" "commit"]
   ["vcs" "push"]]

  :dependencies
  []

  :profiles
  {:dev
   {:dependencies
    [[org.clojure/clojure "1.10.1"]]}})
