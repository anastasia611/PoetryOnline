<script>
    import IconButton from "./IconButton.svelte";

    const confirmTitle = 'Готово';
    const rejectTitle = 'Закрыть';

    export let open = false;
    export let text = false;
    export let showConfirm = true;
    export let onConfirm;
    export let onReject;
</script>

{#if open}
    <div class="tooltip">
        <div class="tooltip-arrow"></div>
        {text}
        <div class="tooltip-bar">
            {#if showConfirm}
                <div class="tooltip-button left">
                    <IconButton icon="check" height="30" width="30" padding="6"
                                title={confirmTitle} opacity={1} hoverOpacity={0.7}
                                round fill color="white" backColor="#4fd173"
                                on:click={onConfirm}/>
                </div>
            {/if}
            <div class="tooltip-button">
                <IconButton icon="cross" height="30" width="30" padding="6"
                            title={rejectTitle} opacity={1} hoverOpacity={0.7}
                            round borders
                            on:click={onReject}/>
            </div>
        </div>
    </div>
{/if}

<style lang="scss">
  .tooltip {
    --color: #ffffff;

    position: absolute;
    transform: translate(0, -100%);
    top: 0;
    left: -0.25rem;
    min-width: 15rem;
    background-color: var(--color);
    padding: 0.75rem 0.25rem 0.25rem 0.75rem;
    box-shadow: 0 4px 6px 0 #b7b1ac;
    text-align: left;
    font-size: .8em;
    z-index: 2;
    box-sizing: border-box;
    border-radius: 0.25rem;

    & .tooltip-arrow {
      &::before {
        border-width: 0.6rem 0.45rem 0 0.45rem;
        border-left-color: transparent;
        border-right-color: transparent;
        border-bottom-color: transparent;
        top: calc(100%);
        content: '';
        display: block;
        width: 0;
        height: 0;
        border-style: solid;
        position: absolute;
        color: var(--color);
      }
    }

    & .tooltip-bar {
      display: flex;
      justify-content: center;
      margin-top: 0.75rem;
      margin-bottom: 0.3rem;
    }

    & .tooltip-button {
      &.left {
        margin-right: 1rem;
      }
    }
  }

</style>