#!/usr/bin/env bb

(ns merge-and-process
  (:require [babashka.process :as process]))

(def usage-message
  (str "Usage: merge_and_process <from-role> <commit-abbrev>\n\n"
       "Fast-forward merges the provided commit into the current branch/worktree."))

(defn fail! [message]
  (binding [*out* *err*]
    (println message))
  (System/exit 1))

(defn sh-ok? [& args]
  (zero? (:exit (apply process/sh (concat [{:continue true}] args)))))

(defn commit-valid? [commit]
  (and (re-matches #"[0-9a-fA-F]{10}" commit)
       (sh-ok? "git" "rev-parse" "--verify" (str commit "^{commit}"))))

(defn merge! [commit]
  (let [result (process/sh {:continue true} "git" "merge" "--ff-only" commit)]
    (when-not (zero? (:exit result))
      (fail! (str "merge_and_process failed to merge commit " commit "\n"
                  (:err result) (:out result))))))

(defn -main [& args]
  (let [[_from-role commit & extra] args]
    (when (or (nil? commit) (seq extra))
      (fail! usage-message))
    (when-not (commit-valid? commit)
      (fail! (str "Invalid commit abbreviation: " commit " (expected 10 hex characters resolving to a commit)")))
    (merge! commit)))

(apply -main *command-line-args*)
