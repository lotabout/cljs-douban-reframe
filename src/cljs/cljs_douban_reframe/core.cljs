(ns cljs-douban-reframe.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [cljs-douban-reframe.handlers]
              [cljs-douban-reframe.subs]
              [cljs-douban-reframe.views :as views]))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init [] 
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch [:query-channel])
  (re-frame/dispatch [:next-song])
  (mount-root))
