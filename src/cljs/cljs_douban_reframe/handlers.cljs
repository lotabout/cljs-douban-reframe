(ns cljs-douban-reframe.handlers
    (:require [re-frame.core :as re-frame]
              [cljs.core.async :refer [<! chan >!]]
              [cljs-douban-reframe.db :as db]
              [cljs-douban-reframe.rpc :as rpc])
    (:require-macros [cljs.core.async.macros :refer [go]]))

(re-frame/register-handler
 :initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/register-handler
 :query-channel
 (fn [db _]
   "send request for channel list"
   #_(.log js/console (str "query-channel " (deref (re-frame/subscribe [:db]))))
   (rpc/get-channel-list)
   db))

(re-frame/register-handler
 :receive-channel
 (fn [db [_ channel-list]]
   "save the channel list to the database"
   #_(.log js/console (str "receive-channel " (deref (re-frame/subscribe [:db]))))
   (assoc db :channel-list (channel-list "channels"))))

(defn get-next-song [db]
  (let [song-list (:song-list db)]
    (assoc db
           :song-list (pop song-list)
           :current-song (peek song-list))))

(defn get-next-song-remote [db]
  (go
    (re-frame/dispatch-sync
     [:receive-song ((<! (rpc/song-op "n" :channel (:current-channel-id db))) "song")])
    #_(.log js/console (str "after go " (deref (re-frame/subscribe [:db]))))
    (re-frame/dispatch-sync [:next-song]))
  db)

(re-frame/register-handler
 :receive-song
 (fn [db [_ song-list]]
   (assoc db :song-list song-list)))

(re-frame/register-handler
 :next-song
 (fn [db [_]]
   (let [song-list (:song-list db)]
     (if (>= (count song-list) 1)
       (get-next-song db)
       (get-next-song-remote db)))))

(re-frame/register-handler
 :end-song
 (fn [db [_]]
   (rpc/song-op "e" :sid (:current-song db))
   (re-frame/dispatch [:next-song])))

(re-frame/register-handler
 :skip-song
 (fn [db [_]]
   (rpc/song-op "s" :sid (:current-song db))
   (re-frame/dispatch [:next-song])))

(re-frame/register-handler
 :delete-song
 (fn [db [_]]
   (rpc/song-op "b" :sid (:current-song db))
   (re-frame/dispatch [:next-song])))

(re-frame/register-handler
 :rate-song
 (fn [db [_]]
   (if (= (get-in db [:current-song "like"]) 0)
     (do
       (rpc/song-op "r" :sid (:current-song db))
       (assoc-in db [:current-song "like"] 1))
     (do
       (rpc/song-op "u" :sid (:current-song db))
       (assoc-in db [:current-song "like"] 0)))))
