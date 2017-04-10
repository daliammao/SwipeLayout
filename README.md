# SwipeLayout

滑动布局，实现四个方向的抽屉式切换，有联动性和层次性两种展现方式。

[APK下载](https://github.com/daliammao/SwipeLayout/raw/master/app/apk/app-debug.apk)

<div class='row'>
    <img src='https://raw.githubusercontent.com/daliammao/SwipeLayout/master/image1.png' width="300px"/>
    <img src='https://raw.githubusercontent.com/daliammao/SwipeLayout/master/image2.png' width="300px"/>
</div>

还可以实现类似ios左滑出现按钮的效果
只要SwipeEnabledHandler接口，即可处理可滑动子view与SwipeLayout的滑动冲突（demo中有实现）


感谢daimajia，该项目基于[AndroidSwipeLayout](https://github.com/daimajia/AndroidSwipeLayout)进行改造，我主要修改了滑动禁止机制，使得在多个列表间进行切换也不会发生冲突。并且删除了原有项目中的列表滑动功能，因为我觉得每个item自己去处理开启状态，项目只提供滑动的功能就够了。