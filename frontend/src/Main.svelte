<script>
    import Poem from "./Poem.svelte";
    import Helper from "./Helper.svelte";

    export let stanzas = [];

    const title = 'Поэт.Онлайн';

    let tooltipIndex = 0;
    let isTooltipOpen = false;

    const onKeyDown = (e) => {
        if (open) {
            if (e.key === 'Escape') {
                isTooltipOpen = 0;
            } else if (e.key === 'Enter') {
                tooltipIndex++;
            } else if (e.key === 'ArrowLeft') {
                tooltipIndex--;
            } else if (e.key === 'ArrowRight') {
                tooltipIndex++;
            }
        }
    };

</script>

<header on:keydown|preventDefault={onKeyDown}>
    {title}
    <ul>
        <li></li>
        <li>
            <Helper bind:open={isTooltipOpen} bind:tooltipIndex />
        </li>
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
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0;
    padding: 1rem 1rem;
    height: 1.5rem;
    width: 100%;
    font-size: 1.5rem;
    background-color: #F4C8BC;
    box-shadow: 0 4px 6px 0 #b7b1ac;
    z-index: 3;

    & ul {
      display: flex;
      list-style: none;
      margin-right: 2rem;
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

  #overlay {
    position: fixed;
    display: none;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 2;
    cursor: pointer;
  }
</style>
