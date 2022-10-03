(ns eugene.graph.editor.history
  "Functions for manipulations with the history of operations.")

(comment (def history
           {:states  [{:graph          {:nodes            #{0 1 2}
                                        :node->neighbours {0 #{1}
                                                           1 #{2}}}
                       :node->position {0 [100 100]
                                        1 [150 100]
                                        2 [200 250]}
                       :selection      {:nodes [1]
                                        :area  {:from [0 0]
                                                :to   [100 100]}
                                        :edges #{[0 1]
                                                 [1 2]}}}]
            :current 0}))

(defn rewind [history index]
  (cond-> history
          (contains? (:states history) index) (assoc :current index)))

(defn undo [{:keys [current]
             :or   {current 0}
             :as   history}]
  (rewind history (dec current)))

(defn redo [{:keys [current]
             :or   {current 0}
             :as   history}]
  (rewind history (inc current)))

(defn current-state [{:keys [states current]}]
  (get states current))

(defmulti advance
          (fn [_history mode _f & _args]
            mode))

(defmethod advance :default [{:keys [states current]
                              :or   {states  [nil]
                                     current 0}
                              :as   history}
                             _mode
                             f & args]
  (let [current-state (current-state history)
        new-index (inc current)
        new-state (apply f current-state args)
        new-state (assoc new-state :last-op f)]
    {:states  (conj (subvec states 0 new-index) new-state)
     :current new-index}))

(defmethod advance :on-change [{:keys [states current]
                                :or   {states  [nil]
                                       current 0}
                                :as   history}
                               _mode
                               f & args]
  (let [current-state (-> (current-state history)
                          (dissoc :last-op))
        new-index (inc current)
        new-state (apply f current-state args)
        new-state-with-op (assoc new-state :last-op f)]
    (if (= new-state current-state)
      history
      {:states  (conj (subvec states 0 new-index) new-state-with-op)
       :current new-index})))

(defmethod advance :stack [{:keys [states current]
                            :or   {states  [nil]
                                   current 0}
                            :as   history}
                           _mode
                           f & args]
  (let [current-state (current-state history)
        last-op (:last-op current-state)
        ;; if we're applying the same operation - modify 'current history position'
        ;; instead of creating new one:
        new-index (cond-> current
                          (not= f last-op) inc)
        new-state (apply f current-state args)
        new-state (assoc new-state :last-op f)]
    {:states  (conj (subvec states 0 new-index) new-state)
     :current new-index}))