package com.rtlabs.reqtool.model.requirements;

import com.rtlabs.reqtool.model.requirements.impl.RequirementsFactoryImpl;

public class RequirementsFactoryImpFinal extends RequirementsFactoryImpl {

	@Override
	public Specification createSpecification() {
		SpecificationImplFinal specification = new SpecificationImplFinal();
		return specification;
	}

}
