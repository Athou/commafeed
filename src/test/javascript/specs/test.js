describe("A suite", function() {
	it("contains spec with an expectation", function() {
		expect(true).toBe(true);
	});
});

describe("A suite is just a function", function() {
	var a;

	it("and so is a spec", function() {
		a = true;

		expect(a).toBe(true);
	});
});