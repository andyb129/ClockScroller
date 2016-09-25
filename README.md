# Clock Scroller

A cool animated RecyclerView clock face scroller handle inspired by the following [MaterialUp](https://material.uplabs.com/) submission ~>

<a href="https://material.uplabs.com/posts/codepen-scrolling-clock">https://material.uplabs.com/posts/codepen-scrolling-clock</a>

* Scroll handle shows animated clock face when scrolling
* Clock face animates to hours
* Still a WORK IN PROGRESS so please submit a pull request if you can improve/find bugs! :-)

<p>
<a href="https://play.google.com/store/apps/details?id=uk.co.barbuzz.clockscroller.sample"><img src="https://github.com/andyb129/ClockScroller/blob/master/screenshots%2Fgoogle_play_badge.png" height="80" width="210" alt="ClockScroller"/></a>
</p>
<p>
<img src="https://github.com/andyb129/ClockScroller/blob/master/screenshots%2Fclock_scroller_anim.gif" height="600" alt="ClockScroller"/>
</p>
  
<!--![optional caption text](screenshots/beer_progress_view_anim.gif)-->

### Setup
To use **Clock Scroller** in your projects, simply add the library as a dependency to your build.

##### Gradle
```
dependencies {
  compile 'uk.co.barbuzz:clockscroller:0.0.1'
}
```

##### Maven
```
<dependency>
  <groupId>uk.co.barbuzz.clockscroller</groupId>
  <artifactId>beerprogressview</artifactId>
  <version>0.0.1</version>
  <type>pom</type>
</dependency>
```

Alternatively you can directly import the /library project into your Android Studio project and add it as a dependency in your build.gradle.

The library is currently configured to be built via Gradle only. It has the following dependencies:

* RecyclerView              - com.android.support:recyclerview-v7
* Compiled SDK Version      - marshmallow-24
* Minimum SDK Version       - jelly bean-16

### Usage
For more detailed code example to use the library, Please refer to the `/sample` app.

Add the `DateGetter` interface to your RecyclerView and implement `getDateFromAdapter()` method to 
return a date from the position of the data set in your RecyclerView e.g.

```
@Override
public Date getDateFromAdapter(int pos) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(dataSet.get(pos).time.substring(0,2)));
    cal.set(Calendar.MINUTE, 0);
    return cal.getTime();
}
```

Then create your RecyclerView instance and create a FastScroller instance and pass in the RecyclerView to it e.g.

```
mContactsRecyclerView = (RecyclerView) findViewById(R.id.contacts_recycler_view);
mContactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
mAdapter = new RecyclerViewAdapter(calendarTimeSlotsList, this);
mContactsRecyclerView.setAdapter(mAdapter);

FastScroller fastScroller = (FastScroller) findViewById(R.id.fast_scroller_view);
fastScroller.setRecyclerView(mContactsRecyclerView);
```

### TODO
1. Work out how to add minutes to the scrolling clock
2. Iron out any bugs

### Thanks

This library has been made by pulling together two other open source libraries to get what I needed at the time. So huge thanks to the following libraries which this is based on.

* Sticky Index by [edsilfer](https://github.com/edsilfer) - [https://github.com/edsilfer/sticky-index](https://github.com/edsilfer/sticky-index)
* Clock Drawable Animation by [evelyne24](https://github.com/evelyne24) - [https://github.com/evelyne24/ClockDrawableAnimation](https://github.com/evelyne24/ClockDrawableAnimation)

### Licence
```
Copyright (c) 2016 Andy Barber

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
