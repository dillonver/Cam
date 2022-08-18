package com.dillon.supercam.utils.view.leonids.modifiers;


import com.dillon.supercam.utils.view.leonids.Particle;

public interface ParticleModifier {

	/**
	 * modifies the specific value of a particle given the current miliseconds
	 * @param particle
	 * @param miliseconds
	 */
	void apply(Particle particle, long miliseconds);

}
