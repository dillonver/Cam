package com.dillon.supercam.utils.view.leonids.initializers;


import com.dillon.supercam.utils.view.leonids.Particle;

import java.util.Random;

public class RotationSpeedInitializer implements ParticleInitializer {

	private final float mMinRotationSpeed;
	private final float mMaxRotationSpeed;

	public RotationSpeedInitializer(float minRotationSpeed,	float maxRotationSpeed) {
		mMinRotationSpeed = minRotationSpeed;
		mMaxRotationSpeed = maxRotationSpeed;
	}

	@Override
	public void initParticle(Particle p, Random r) {
		float rotationSpeed = r.nextFloat()*(mMaxRotationSpeed-mMinRotationSpeed) + mMinRotationSpeed;
		p.mRotationSpeed = rotationSpeed;
	}

}
