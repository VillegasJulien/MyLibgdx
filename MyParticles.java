import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Timer;
import com.space.invaders3d.Object.CustomModelLoader;

import java.util.HashMap;
import java.util.Iterator;

interface EffectCallback {
    public void finish(ParticleEffect effect);
}

public class MyParticles implements EffectCallback {
    public static class PFXPool extends Pool<ParticleEffect> {
        public ParticleEffect sourceEffect;

        public PFXPool(ParticleEffect SourceEffect) {
            sourceEffect = SourceEffect;
        }

        /*
        Can causes a memory leak
        @Override
        public void free(ParticleEffect pfx) {
            pfx.reset();
            super.free(pfx);
        }*/

        @Override
        public ParticleEffect newObject() {
            return sourceEffect.copy();
        }

        public void dispose(){
            sourceEffect.dispose();
        }
    }

    public final ParticleSystem particleSystem;
    private final PointSpriteParticleBatch particleBatch;
    public final ParticleEffectLoader.ParticleEffectLoadParameter loadParam;
    public final ParticleEffectLoader loader;

    public HashMap<String, PFXPool> pool = new HashMap<String, PFXPool>();

    public MyParticles(PerspectiveCamera camera3d){
        particleSystem = ParticleSystem.get();

        particleBatch = new PointSpriteParticleBatch();
        particleBatch.setCamera(camera3d);
        particleSystem.add(particleBatch);

        loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
        loader = new ParticleEffectLoader(new InternalFileHandleResolver());
    }

    public void loadPool(HashMap<String, String> poolList, AssetManager assets){
        Iterator it = poolList.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<String, String> pairs = (HashMap.Entry<String, String>)it.next();
            pool.put(pairs.getKey(), new PFXPool(assets.get(pairs.getValue(), ParticleEffect.class)));
            it.remove();
        }
    }

    public void render() {
        particleSystem.update();
        particleSystem.begin();
        particleSystem.draw();
        particleSystem.end();
    }

    public void translate(ParticleEffect effect, Vector3 position){
        Matrix4 targetMatrix = new Matrix4();
        targetMatrix.idt();
        targetMatrix.setToTranslation(new Vector3(position));
        effect.setTransform(targetMatrix);
    }

    public ParticleEffect createEffect(PFXPool pool, Vector3 pos){
        final ParticleEffect effect = pool.newObject();
        effect.init();
        effect.start();
        translate(effect, pos);
        particleSystem.add(effect);
        return effect;
    }

    public void free(final ParticleEffect effect, float timer){
        Emitter emitter = effect.getControllers().first().emitter;
        if (emitter instanceof RegularEmitter) {
            RegularEmitter reg = (RegularEmitter) emitter;
            reg.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
            //reg.durationValue.setLow(10f);
            reg.dispose();
        }
        emitter.dispose();

        //start dirty code
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                finish(effect);
            }
        }, timer);
        //end dirty code
    }

    @Override
    public void finish(ParticleEffect effect) {
        particleSystem.remove(effect);
        effect.dispose();
    }

    public void dispose(){
        particleSystem.removeAll();

        Iterator it = pool.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<String, PFXPool> pairs = (HashMap.Entry<String, PFXPool>)it.next();
            pairs.getValue().dispose();
            it.remove();
        }

        pool.clear();
    }

}
