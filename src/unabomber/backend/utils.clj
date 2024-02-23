(ns unabomber.backend.utils
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn get-nodejs-bin-dir
  "Returns the location of the node bin dir where usually npm and node executables reside. Or throws an error."
  []
  (let [possibly-nvm? (->> "/.nvm/nvm.sh"
                           (str (System/getProperty "user.home"))
                           io/file
                           .exists)
        path (-> (ProcessBuilder. ["/bin/bash"
                                   "-l" "-c"
                                   (str
                                    (when possibly-nvm?
                                      "source ~/.nvm/nvm.sh && ")
                                    "which npm")])
                 (.start)
                 (.getInputStream)
                 (java.io.InputStreamReader.)
                 (java.io.BufferedReader.)
                 (slurp)
                 (str/trim)
                 (java.io.File.)
                 (.getParent))]
    (if (str/blank? path)
      (throw (Exception. "nodejs binaries not found in $PATH!"))
      path)))
