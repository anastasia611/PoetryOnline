<script>
    import Poem from "./Poem.svelte";

    export let stanzas = [];

    const title = 'Поэт.Онлайн';

</script>

<header>
    {title}
    <ul>
        <li></li>
        <li></li>
    </ul>
</header>
<main>
    <div class="container">
        <Poem {stanzas}/>
    </div>
</main>

<style lang="scss">
    header {
        position: fixed;
        top: 0;
        padding: 1rem 1rem;
        height: 1.5rem;
        width: 100%;
        font-size: 1.5rem;
        background-color: #F4C8BC;
        z-index: 3;

        & ul {
            list-style: none;
        }
    }

    main {
        margin-top: 4rem;
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 0.5em;
    }

    .container {
        box-shadow: 0 6px 8px 0 #b7b1ac;
        min-width: 75%;
        background-color: white;
    }

</style>
