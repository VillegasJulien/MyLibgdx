libgdx
======

My libgdx custom class/helper ...


Simple exemple to use MyParticles : 

```
//instantiate
myParticles = new MyParticles(camera3d);

//load assets
assets.setLoader(ParticleEffect.class, myParticles.loader);
assets.load("particles/explosion1.part", ParticleEffect.class, myParticles.loadParam);
assets.load("particles/explosion2.part", ParticleEffect.class, myParticles.loadParam);
assets.finishLoading();

//load pooler
HashMap<String, String> particle = new HashMap<String, String>();
particle.put("explosion_small", "particles/explosion1.part");
particle.put("explosion_big", "particles/explosion2.part");
myParticles.loadPool(particle, assets);
particle.clear();

//create particles ( array is better )
exploseSmall = myParticles.createEffect(myParticles.pool.get("explosion_small"));
exploseBig = myParticles.createEffect(myParticles.pool.get("explosion_small"));

//render methode
modelBatch.begin(camera);
myParticles.render();
modelBatch.render(myParticles.particleSystem);
modelBatch.end();

//dispose effect with timer
myParticle.free(exploseSmall, 0.5f);
myParticle.free(exploseBig, 1);

//dispose myparticle system and pooler
myParticle.dispose();
```
