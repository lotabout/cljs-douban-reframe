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
   (rpc/get-channel-list)
   db))

(re-frame/register-handler
 :receive-channel
 (fn [db [_ channel-list]]
   "save the channel list to the database"
   (assoc db :channel-list (channel-list "channels"))))

(defn get-next-song [db]
  (let [song-list (:song-list @db)]
    (assoc db
           :song-list (pop song-list)
           :current-song (peek song-list))))

(defn get-next-song-remote [db]
  (go (re-frame/dispatch-sync
       [:reset-db (assoc db :song-list ((<! (rpc/song-op "n" :channel (:current-channel-seq-id @db))) "song"))])
      (re-frame/dispatch-sync [:next-song]))
  db)

(re-frame/register-handler
 :reset-db
 (fn [db [_ new-db]]
   new-db))

(re-frame/register-handler
 :next-song
 (fn [db [_]]
   (let [song-list (:song-list @db)]
     (if (>= (count song-list) 1)
       (get-next-song db)
       (get-next-song-remote db)))))
