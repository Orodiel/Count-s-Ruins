(ns eugene.graph.editor.core
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache]
            [eugene.graph.core :as graph]
            [eugene.graph.visual :as visual]
            [eugene.graph.editor.handlers :refer [event-handler]]
            [eugene.graph.editor.history :as history]
            [eugene.graph.editor.views :as views]))

(def *context
  (atom (fx/create-context
          (-> nil
              (history/advance nil visual/create-node [100 100])
              (history/advance nil visual/create-node [200 200])
              (history/advance nil visual/create-node [100 200])
              (history/advance nil update :graph graph/add-edge [1 2])
              (history/advance nil update :graph graph/add-edge [0 1])
              (history/advance nil visual/select-edge [1 2])
              (history/advance nil visual/select-node 1))
          cache/lru-cache-factory)))

(def *mouse-position
  (atom [0 0]))

(defonce editor
         (fx/create-app *context
                        :effects {:mouse (fx/make-reset-effect *mouse-position)}
                        :co-effects {:mouse (fx/make-deref-co-effect *mouse-position)}
                        :event-handler #'event-handler
                        :desc-fn (fn [_] {:fx/type views/root-view})))

(comment
  (:cljfx.context/m (deref *context))
  ((:renderer editor)))