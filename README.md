NTUVote 身分驗證系統（選務端）
==============================

這是用於電子投票的身分驗證系統選務端程式，於臺灣大學 103 至 104 學年度學生會長選舉、學代會學生代表選舉配合[伺服器端](https://github.com/rschiang/ntu-vote-auth-server)使用。

完整電子投票系統架構分為驗證與投票兩部分，其中裝置整合與身分驗證系統由[臺灣大學學生會選舉罷免執行委員會](https://www.facebook.com/NTUVote)委託[臺灣大學開源社](https://ntuosc.org) [RSChiang](https://github.com/rschiang/ntu-vote-auth-server) 規劃研發；投票系統則延請 [MouseMs](https://github.com/mousems/NTUvoteV2) 實作。

此專案以 [Apache 2.0](LICENSE.md) 授權釋出供公眾使用。

系統需求 / System Requirements
-----------------------------

身分驗證系統用戶端使用 Android Studio 建置，可以在支援 MiFare NFC 的任何 Android 4.0.3 Ice Cream Sandwich 或更高版本的行動裝置上執行。

NTU Vote Authentication Server
------------------------------

This project is the authentication client that was used in the 2014 ~ 2016 [NTU Student Association](https://ntustudents.org) and [NTU Student Council](http://ntusc.org) Representative Election, in conjunction with [authentication server](https://github.com/rschiang/ntu-vote-auth-server). Tailored for [NTU Election Execution Commission](https://www.facebook.com/NTUVote) staff, this application can run on any mobile device supporting MiFare NFC and running Android 4.0.3 Ice Cream Sandwich or higher.

The full e-vote architecture consists of two distinguish parts: authentication and ballot-casting. The authentication and device integration part is done by [NTU Open Source Community](https://ntuosc.org) under the delegation of [NTU Students' Association Election Commission](https://www.facebook.com/NTUVote), while [MouseMs](https://github.com/mousems/NTUvoteV2) from NTUST is in charge of the voting system.

This project is released under [Apache License 2.0](LICENSE.md).
