(ns build
  (:require [clojure.java.shell :refer [sh]]
            [clojure.tools.build.api :as b]))

(def lib 'unabomber/unabomber)
;; I don't care for the version number at this point, someday I may
;; (def version (format "1.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:aliases [:backend :frontend]}))
(def jar-file (format "target/%s.jar" (name lib)))

(defn build-cljs+css [_]
  (println "BUILD: Compiling Clojurescript files")
  ;; first pass we have to do without any optimization, so postcss could figure out
  ;; tailwind classes
  (println "BUILD: Prepping cljs for CSS processing...")
  (println (:out (sh "npx" "shadow-cljs" "compile" ":app")))

  (println "BUILD: Processing CSS...")
  (println (:out (sh "npm" "run-script" "postcss:build")))

  (println "BUILD: Compiling cljs files with optimization...")
  (println (:out (sh "npx" "shadow-cljs" "release" ":release")))

  (println "BUILD: Cleaning up...")
  (b/delete {:path "resources/public/out/cljs-runtime"}))

(defn uberjar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version nil
                :basis basis
                :src-dirs ["src"]})
  (build-cljs+css nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (println "BUILD: Producing jar:" jar-file "...")
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file jar-file
           :basis basis
           :manifest {"Main-Class" "unabomber.backend.system"}})
  (println "BUID: Done building jar."))
