import App from './Main.svelte';

const app = new App({
	target: document.body,
	props: {
		words: ['Я', 'к', 'вам', 'пишу', 'd', 'd', 'g', 't', 'h', 'g', 'd', 'd', 'g', 't', 'h', 'g']
	}
});

export default app;