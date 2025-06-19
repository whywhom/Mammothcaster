# 项目结构 / Project Structure

## UI层 / UI Layer
- 包括 Podcast 列表、Episode 列表以及播放控件。  
- Includes the podcast list, episode list, and playback controls.

## 网络层 / Network Layer
- 使用 OkHttp 进行 API 请求。  
- Uses OkHttp to perform API requests.

## 数据层 / Data Layer
- 使用 Room 作为持久存储，更新数据。  
- Uses Room as the persistent storage solution to update data.

## 音频播放 / Audio Playback
- 通过 ExoPlayer 播放内容，并支持后台播放。  
- Uses ExoPlayer to play audio content and supports background playback.

## RSS解析 / RSS Parsing
- 使用 Rome 从播客订阅源中解析数据。  
- Uses Rome to parse data from podcast RSS feeds.

# Branches

main - release app

develop - developer merge branch to develop

# Home Screen

![](screenshots/homescreen.png)

## Detail Screen

![](screenshots/detailscreen.png)

## Home Screen Library

![](screenshots/homescreenwithsubscribe.png)

## Play Screen

![](screenshots/playscreen.png)

## Notification Player

![](screenshots/notificationui.png)
