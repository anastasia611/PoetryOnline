<script>
    import Stanza from "./Stanza.svelte";

    export let stanzas = [];

    const title = 'Поэт.Онлайн';

    let newWord = '';

</script>

<header>
    {title}
    <ul>
        <li></li>
        <li></li>
    </ul>
</header>
<main>
    <div class="container" id="container1">
        <div class="poem">
<!--            <h1>Стихотворение</h1>-->
            {#each stanzas as lines}
               <Stanza {lines} />
            {/each}
        </div>
    </div>
</main>

<style lang="scss">
    header {
        padding: 0.5rem 1rem;
        font-size: 1rem;

        & ul {
            list-style: none;
        }
    }

    main {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 0.5em;
        margin: 0 auto;
    }

    h1 {
        color: #DE8484;
    }

    .container {
        box-shadow: 0 6px 8px 0 #b7b1ac;
        width: 75%;
        background-color: white;

    }

    .poem {
        display: flex;
        width: fit-content;
        flex-direction: column;
        margin-left: auto;
        margin-right: auto;
        padding: 1rem;
    }

</style>
