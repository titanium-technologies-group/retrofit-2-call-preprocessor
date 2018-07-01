RxPremapper
=========
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://github.com/titanium-codes/LocGetter/blob/master/LICENSE)
[ ![Download](https://api.bintray.com/packages/titanium-codes/Android/rxpremapper/images/download.svg) ](https://bintray.com/titanium-codes/Android/rxpremapper/_latestVersion)

Overview
--------
Rx java 1 call adapter for retrofit 2 to process object before emiting them further.

Install
-------
#### Gradle

**Step 1.** Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        jcenter()
    }
}
```
**Step 2.** Add the dependency
```
compile 'codes.titanium:rxpremapper:1.0.0'
```

Basic Usage
-----------

Main features:
* Process every entity you want with same way before emiting it to subscriber
* Easy setup

Example:
Create some FlatMappers
```
FlatMapper<TestEntity> flatMapper = new FlatMapper<TestEntity>() {
      @Override
      public Class<TestEntity> getResponseClass() {
        //define class
        return TestEntity.class;
      }

      @Override
      public Observable<TestEntity> flatMapInto(Observable<TestEntity> source) {
        //preprocess observable here
        return source.doOnNext(testEntity -> {
          //save to database
          //some business logic
        });
      }
    };
```
Add all of them into list:
```
List<FlatMapper> flatMappers = new ArrayList();
flatMappers.add(firstEntityFlatMapper);
flatMappers.add(secondEntityFlatMapper);
```
Init retrofit client:
```
 Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://example.com")
        .client(client)
        .addCallAdapterFactory(RequestPreprocessRxJavaAdapter.createWithScheduler(Schedulers.io(), flatMappers))
        .build();
```

Release notes
-------------
### 1.0.0
> * Initial release